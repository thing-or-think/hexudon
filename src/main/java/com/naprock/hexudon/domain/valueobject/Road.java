package com.naprock.hexudon.domain.valueobject;

public class Road {

    private Cell cellFrom;
    private Cell cellTo;

    public Road() {
    }

    public Road(Cell cellFrom, Cell cellTo) {
        this.cellFrom = cellFrom;
        this.cellTo = cellTo;
    }

    public Cell getCellFrom() {
        return cellFrom;
    }

    public void setCellFrom(Cell cellFrom) {
        this.cellFrom = cellFrom;
    }

    public Cell getCellTo() {
        return cellTo;
    }

    public void setCellTo(Cell cellTo) {
        this.cellTo = cellTo;
    }

}
