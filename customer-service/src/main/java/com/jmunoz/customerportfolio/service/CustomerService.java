package com.jmunoz.customerportfolio.service;

import com.jmunoz.customerportfolio.dto.CustomerInformation;
import com.jmunoz.customerportfolio.entity.Customer;
import com.jmunoz.customerportfolio.exceptions.ApplicationExceptions;
import com.jmunoz.customerportfolio.mapper.EntityDtoMapper;
import com.jmunoz.customerportfolio.repository.CustomerRepository;
import com.jmunoz.customerportfolio.repository.PortfolioItemRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PortfolioItemRepository portfolioItemRepository;

    public CustomerService(CustomerRepository customerRepository, PortfolioItemRepository portfolioItemRepository) {
        this.customerRepository = customerRepository;
        this.portfolioItemRepository = portfolioItemRepository;
    }

    public Mono<CustomerInformation> getCustomerInformation(Integer customerId) {
        return this.customerRepository.findById(customerId)
                .switchIfEmpty(ApplicationExceptions.customerNotFound(customerId))
                .flatMap(this::buildCustomerInformation);
    }

    private Mono<CustomerInformation> buildCustomerInformation(Customer customer) {
        // Si no se encuentra nada se devuelve una lista vacía, lo que es correcto.
        return this.portfolioItemRepository.findAllByCustomerId(customer.getId())
                .collectList()
                .map(list -> EntityDtoMapper.toCustomerInformation(customer, list));
    }
}
