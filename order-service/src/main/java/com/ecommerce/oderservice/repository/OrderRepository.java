package com.ecommerce.oderservice.repository;

import com.ecommerce.oderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
