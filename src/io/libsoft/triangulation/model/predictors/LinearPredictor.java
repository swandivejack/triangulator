package io.libsoft.triangulation.model.predictors;

import io.libsoft.triangulation.model.Sensor;
import io.libsoft.triangulation.model.utils.Position;
import io.libsoft.triangulation.model.utils.Vector;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class LinearPredictor implements Predictor {


  private final double ALPHA = 1;

  private Position position;
  private Vector velocity;
  private Position target;
  private Deque<Position> history;
  private Deque<Position> targetPositions;
  private Vector prediction;
  private Sensor sensor;
  private boolean running;

  public LinearPredictor(Sensor sensor) {
    this.sensor = sensor;
    history = new LinkedList<>();
    targetPositions = new LinkedList<>();
    position = Position.ZERO();
    velocity = Vector.ZERO();
    target = Position.ZERO();

  }

  public List<Position> getHistory() {
    return new ArrayList<>(history);
  }

  @Override
  public void update() {

  }

  private void makePrediction() {
    double sumX = 0.0;
    double sumX2 = 0.0;
    double sumY = 0.0;
    for (Position position : targetPositions) {
      sumX += position.getX();
      sumX2 += position.getX() * position.getX();
      sumY += position.getY();
    }
    double xBar = sumX / targetPositions.size();
    double yBar = sumY / targetPositions.size();

    double XXbar = 0.0;
    double YYbar = 0.0;
    double XYbar = 0.0;

    for (Position position : targetPositions) {
      XXbar += Math.pow(position.getX() - xBar, 2);
      YYbar += Math.pow(position.getY() - yBar, 2);
      XYbar += (position.getX() - xBar) * (position.getY() - yBar);
    }
    double slope = XYbar / XXbar;

    double theta = -Math.atan2(slope *
            Math.signum(targetPositions.peekLast().getX() - targetPositions.peekFirst().getX()),
        Math.signum(targetPositions.peekFirst().getX() - targetPositions.peekLast().getX()));
    Position end = targetPositions.peekFirst();
    Position start = targetPositions.peekLast();

    double r = Math.sqrt(Math.pow(end.getX() - start.getX(), 2) +
        Math.pow(end.getY() - start.getY(), 2));
    double delta = end.getTimeStamp() - start.getTimeStamp();
    r *= delta;


//    System.out.println(delta);
    if (Double.isNaN(theta)) {
      prediction = null;
    } else {
      prediction = Vector.fromPolar(theta, r);
    }
  }

  public void setTarget(Position target) {
    targetPositions.push(target);
    history.add(position);
    if (targetPositions.size() > 3) {
      targetPositions.removeLast();
    }
    if (history.size() > 40) {
      history.removeLast();
    }
  }

  @Override
  public Vector getPredictionVector() {
    return prediction;
  }

  public Position getCurrentPosition() {
    return position;
  }

  @Override
  public void run() {
    running = true;
    while (running) {
      setTarget(sensor.getTarget());
      makePrediction();
      // if the prediction is null, which happens in cases where the last n points
      // are all the same use the last known position as the target
      if (prediction != null) {
        target = Position.at(prediction.getX() + targetPositions.peekFirst().getX(),
            prediction.getY() + targetPositions.peekFirst().getY());
      } else {
        target = targetPositions.peekFirst();
      }
      double xHatNew = ALPHA * (target.getX() - position.getX());
      double yHatNew = ALPHA * (target.getY() - position.getY());
      velocity = Vector.from(xHatNew, yHatNew);
      double newX = position.getX() + velocity.getX();
      double newY = position.getY() + velocity.getY();

      position = Position.at(newX, newY);

      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

  }

}
