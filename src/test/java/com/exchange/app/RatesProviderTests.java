package com.exchange.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RatesProviderTests {

    public static final String SEK = "SEK";
    public static final String USD = "USD";
    public static final String EUR = "EUR";

    private final ForeignExchangeRatesApiClient apiClient = Mockito.mock(ForeignExchangeRatesApiClient.class);
    private final RatesProvider provider = new RatesProvider(apiClient);
    private Map<String, Double> exchangeRates;

    @BeforeEach
    void setUp() {
        exchangeRates = new HashMap() {
        };
    }

    @Test
    @DisplayName("For default currency (EUR) returns all rates")
    void shouldReturnCurrencyExchangeRatesForEUR() {

        //given
        ExchangeRates rates = initializeRates();
        Mockito.when(apiClient.getLatestRates()).thenReturn(rates);

        //when
        Double rateSEK = provider.getExchangeRateInEUR(Currency.getInstance(SEK));
        Double rateUSD = provider.getExchangeRateInEUR(Currency.getInstance(USD));

        //then
        assertAll(
                () -> assertEquals(rates.rates.get(USD), rateUSD, "USD rate should be included"),
                () -> assertEquals(rates.rates.get(SEK), rateSEK, "SEK rate should be included")
        );
    }

    @Test
    void shouldReturnCurrencyExchangeRatesForOtherCurrency() {
        //given
        exchangeRates.put(EUR, 0.8);
        exchangeRates.put(SEK, 15.30);

        ExchangeRates rates = initializeRates(USD, new Date(), exchangeRates);
        Mockito.when(apiClient.getLatestRates(USD)).thenReturn(rates);

        //when
        Double rate = provider.getExchangeRate(Currency.getInstance(SEK), Currency.getInstance(USD));

        //then
        assertTrue(rates.rates.containsKey(SEK));
        assertEquals(rates.rates.get(SEK), rate);
    }

    @Test
    void shouldThrowExceptionWhenCurrencyNotSupported() {
        //given
        Mockito.when(apiClient.getLatestRates()).thenThrow(new IllegalArgumentException());

        //then

        IllegalArgumentException actual =
                assertThrows(IllegalArgumentException.class,
                        () -> provider.getExchangeRateInEUR(Currency.getInstance("CHF")));

        assertEquals("Currency is not supported: CHF", actual.getMessage());
    }

    @Test
    void shouldGetRatesOnlyOnce() {
        //given
        ExchangeRates rates = initializeRates(USD);
        Mockito.when(apiClient.getLatestRates(USD)).thenReturn(rates);

        //when
        provider.getExchangeRate(Currency.getInstance(SEK), Currency.getInstance(USD));

        //then
        Mockito.verify(apiClient, Mockito.times(1)).getLatestRates(USD);
    }

    private ExchangeRates initializeRates() {
        exchangeRates.put(USD, 1.22);
        exchangeRates.put(SEK, 10.30);
        return initializeRates(EUR, new Date(), exchangeRates);
    }

    private ExchangeRates initializeRates(String base) {
        exchangeRates.put(EUR, 1.22);
        exchangeRates.put(SEK, 10.30);
        return initializeRates(base, new Date(), exchangeRates);
    }

    private ExchangeRates initializeRates(String base, Date date, Map<String, Double> rates) {
        return new ExchangeRates(base, date, rates);
    }

}