package com.ecommerce.oderservice.service;

import com.ecommerce.oderservice.dto.InventoryResponse;
import com.ecommerce.oderservice.dto.OrderLineItemsDto;
import com.ecommerce.oderservice.dto.OrderRequest;
import com.ecommerce.oderservice.event.OrderPlacedEvent;
import com.ecommerce.oderservice.model.Order;
import com.ecommerce.oderservice.model.OrderLineItems;
import com.ecommerce.oderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    private final WebClient.Builder webClientBuilder;

    private final Tracer tracer;

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public String placeOrder(OrderRequest orderRequest) throws IllegalAccessException {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(orderLineItemsDto -> mapToDto(orderLineItemsDto))
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream().map(orderLineItem -> orderLineItem.getSkuCode())
                            .toList();

//        System.out.println("-------------------------");
//        System.out.println(skuCodes);
//        System.out.println("-------------------------");
//
        log.info("Calling inventory service");

        Span inventoryServiceLookuo = tracer.nextSpan().name("InventoryServiceLookup");
        try (Tracer.SpanInScope spanInScope = tracer.withSpan(inventoryServiceLookuo.start())) {
            // Call Inventory Service, and place order if product is in stock
            // By default it does an async request ==> we add .block() to make sync
            InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get().uri("http://inventory-service/api/inventory",
                            uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes)
                                    .build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();





            // All elements inside the array has isInstock true
            boolean allProductsInStock = Arrays.stream(inventoryResponseArray).allMatch(inventoryResponse -> inventoryResponse.isInStock());

            if (allProductsInStock){
                orderRepository.save(order);
                kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
                return "Order Placed Successfully";
            } else {
                throw new IllegalAccessException("Product is not in stock please try again later!");
            }

        } finally {
            inventoryServiceLookuo.end();
        }




    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
