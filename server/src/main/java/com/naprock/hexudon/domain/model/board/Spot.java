package com.naprock.hexudon.domain.model.board;

import com.naprock.hexudon.domain.model.geometry.Coordinate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.naprock.hexudon.domain.validation.DomainValidator.*;

public final class Spot {

    private final int brand;
    private final Coordinate pos;
    private final int stocks;
    private final Map<String, Integer> teamStocks;

    public Spot(
            int brand,
            Coordinate pos,
            int stocks
    ) {

        requireNonNegative(brand, "type");
        requireNonNull(pos, "pos");
        requirePositive(stocks, "stocks");

        this.brand = brand;
        this.pos = pos;
        this.stocks = stocks;
        this.teamStocks = new HashMap<>();
    }

    public void registerTeam(String teamId) {
        teamStocks.putIfAbsent(teamId, stocks);
    }

    public void decrementStock(String teamId) {

        Integer stock = teamStocks.get(teamId);

        requireNonNull(stock, "stock");
        requirePositive(stock, "stock");

        teamStocks.put(teamId, stock - 1);
    }

    public Coordinate getPos() {
        return pos;
    }

    public int getBrand() {
        return brand;
    }

    public int getStocks() {
        return stocks;
    }

    public int getStock(String teamId) {
        return teamStocks.getOrDefault(teamId, 0);
    }

    public void resetStocks() {
        teamStocks.replaceAll((teamId, stock) -> stocks);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Spot other)) {
            return false;
        }

        return pos.equals(other.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos);
    }
}