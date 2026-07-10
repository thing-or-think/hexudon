package com.naprock.hexudon.model;

public class Cell {

    private int x;
    private int y;
    private TerrainType terrainType;

    public Cell() {
    }

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }


    public Cell(int x, int y, TerrainType terrainType) {
        this.x = x;
        this.y = y;
        this.terrainType = terrainType;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public void setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
    }
}
