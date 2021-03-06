package io.libsoft.triangulation.model.utils;

public class Position {

  private double x;
  private double y;
  private double timeStamp;

  private Position(double x, double y) {
    this.x = x;
    this.y = y;
    this.timeStamp = System.nanoTime() / 1e9;
  }

  public static Position at(double x, double y){
    return new Position(x,y);
  }

  public static Position ZERO() {
    return Position.at(0,0);
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  @Override
  public String toString() {
    return "Position{" +
        "x=" + x +
        ", y=" + y +
        '}';
  }

  public double getTimeStamp() {
    return timeStamp;
  }

}
