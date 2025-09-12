package com.jmunoz.customerportfolio.service;

import com.jmunoz.customerportfolio.dto.StockTradeRequest;
import com.jmunoz.customerportfolio.dto.StockTradeResponse;
import com.jmunoz.customerportfolio.entity.Customer;
import com.jmunoz.customerportfolio.entity.PortfolioItem;
import com.jmunoz.customerportfolio.exceptions.ApplicationExceptions;
import com.jmunoz.customerportfolio.mapper.EntityDtoMapper;
import com.jmunoz.customerportfolio.repository.CustomerRepository;
import com.jmunoz.customerportfolio.repository.PortfolioItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class TradeService {

    private final CustomerRepository customerRepository;
    private final PortfolioItemRepository portfolioItemRepository;

    public TradeService(CustomerRepository customerRepository, PortfolioItemRepository portfolioItemRepository) {
        this.customerRepository = customerRepository;
        this.portfolioItemRepository = portfolioItemRepository;
    }

    @Transactional
    public Mono<StockTradeResponse> trade(Integer customerId, StockTradeRequest request) {
        return switch (request.action()) {
            case BUY -> this.buyStock(customerId, request);
            case SELL -> this.sellStock(customerId, request);
        };
    }

    private Mono<StockTradeResponse> buyStock(Integer customerId, StockTradeRequest request) {
        // Vamos a tener dos publishers que no se van a ejecutar hasta que alguien se subscriba.

        // Tenemos que tener en cuenta si el usuario tiene suficiente saldo.
        var customerMono = this.customerRepository.findById(customerId)
                .switchIfEmpty(ApplicationExceptions.customerNotFound(customerId))
                .filter(c -> c.getBalance() >= request.totalPrice())
                .switchIfEmpty(ApplicationExceptions.insufficientBalance(customerId));

        // Tenemos que tener en cuenta si hay que insertar un registro en BD o actualizar su quantity.
        var portfolioItemMono = this.portfolioItemRepository.findByCustomerIdAndTicker(customerId, request.ticker())
                .defaultIfEmpty(EntityDtoMapper.toPortfolioItem(customerId, request.ticker()));

        // Aquí es donde realmente ejecutamos la compra de acciones (operaciones en BD)
        // Subscripciones e invocaciones en secuencia.
        // Nos subscribimos a customerMono y, si el customer se ha encontrado,
        // entonces nos subscribimos e invocamos el siguiente publisher.
        // Al igual que zip(), devuelve una tupla.
        return customerMono.zipWhen(customer -> portfolioItemMono)
                .flatMap(t -> this.executeBuy(t.getT1(), t.getT2(), request));
    }

    private Mono<StockTradeResponse> executeBuy(Customer customer, PortfolioItem portfolioItem, StockTradeRequest request) {
        customer.setBalance(customer.getBalance() - request.totalPrice());
        portfolioItem.setQuantity(portfolioItem.getQuantity() + request.quantity());
        return this.saveAndBuildResponse(customer, portfolioItem, request);
    }

    private Mono<StockTradeResponse> sellStock(Integer customerId, StockTradeRequest request) {
        var customerMono = this.customerRepository.findById(customerId)
                .switchIfEmpty(ApplicationExceptions.customerNotFound(customerId));

        // Tenemos que tener en cuenta si el usuario tiene suficientes acciones.
        var portfolioItemMono = this.portfolioItemRepository.findByCustomerIdAndTicker(customerId, request.ticker())
                .filter(pi -> pi.getQuantity() >= request.quantity())
                .switchIfEmpty(ApplicationExceptions.insufficientShares(customerId));

        // Aquí es donde realmente ejecutamos la venta de acciones (operaciones en BD)
        return customerMono.zipWhen(customer -> portfolioItemMono)
                .flatMap(t -> this.executeSell(t.getT1(), t.getT2(), request));
    }

    private Mono<StockTradeResponse> executeSell(Customer customer, PortfolioItem portfolioItem, StockTradeRequest request) {
        customer.setBalance(customer.getBalance() + request.totalPrice());
        portfolioItem.setQuantity(portfolioItem.getQuantity() - request.quantity());
        return this.saveAndBuildResponse(customer, portfolioItem, request);
    }

    private Mono<StockTradeResponse> saveAndBuildResponse(Customer customer, PortfolioItem portfolioItem, StockTradeRequest request) {
        // La construcción de la respuesta apenas conlleva tiempo, pero podría hacerse con un .map en el pipeline
        // tras el zip().
        var response = EntityDtoMapper.toStockTradeResponse(request, customer.getId(), customer.getBalance());

        // Recordar que zip() aceptar varios publishers y devuelve una tupla con el resultado de ambos.
        // zip() invoca los dos publishers a la vez, es decir, no en secuencial, sino en paralelo.
        //
        // Si alguna operación falla se emite una señal de error y no se devuelve response.
        // Y gracias a la anotación @Transactional del méto-do trade() se hará el rollback de
        // lo que se haya podido grabar.
        // Así aseguramos que se hace to-do o nada.
        return Mono.zip(this.customerRepository.save(customer), this.portfolioItemRepository.save(portfolioItem))
                .thenReturn(response);
    }
}
