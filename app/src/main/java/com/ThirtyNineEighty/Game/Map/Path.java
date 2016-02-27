package com.ThirtyNineEighty.Game.Map;

import com.ThirtyNineEighty.Base.Common.Math.Vector;
import com.ThirtyNineEighty.Base.Common.Math.Vector2;
import com.ThirtyNineEighty.Base.Common.Math.Vector3;
import com.ThirtyNineEighty.Base.DeltaTime;
import com.ThirtyNineEighty.Base.Map.IPath;
import com.ThirtyNineEighty.Base.Objects.WorldObject;

import java.util.ArrayList;

public class Path
  implements IPath
{
  private static final long serialVersionUID = 1L;

  private static final float defaultStepCompletion = 1f;
  private static final float influenceOfPivotPoint = 2f;
  private static final float pivotPointLifeTime = 5;

  private final WorldObject<?, ?> object;
  private final ArrayList<Vector2> path;
  private final float stepCompletion;

  private Vector2 pivotPoint;
  private float pivotPointTimeLeft;

  private int currentPathStep;

  public Path(WorldObject<?, ?> object, ArrayList<Vector2> path)
  {
    this.object = object;
    this.path = path;
    this.stepCompletion = object.collidable != null
      ? object.collidable.getRadius()
      : defaultStepCompletion;
  }

  public ArrayList<Vector2> getPath()
  {
    return path;
  }

  @Override
  public float distance()
  {
    float distance = 0.0f;

    Vector3 start = start();
    Vector3 end = end();

    if (start != null && end != null)
    {
      Vector3 vec = end.getSubtract(start);
      distance = vec.getLength();
      Vector.release(vec);
    }

    Vector.release(start);
    Vector.release(end);
    return distance;
  }

  @Override
  public Vector3 start()
  {
    int size = path.size();
    if (size < 1)
      return null;
    return Vector.getInstance(3, path.get(0));
  }

  @Override
  public Vector3 end()
  {
    int size = path.size();
    if (size < 1)
      return null;
    return Vector.getInstance(3, path.get(size - 1));
  }

  public boolean moveObject()
  {
    if (isPivotPointDeprecated())
      return false;

    updatePivotPoint();

    Vector2 movingVector = getMovingVector();
    if (movingVector == null)
      return false;

    float movingAngle = Vector2.xAxis.getAngle(movingVector);
    Vector3 targetAngles = Vector.getInstance(3, 0, 0, movingAngle);
    Vector3 objectAngles = object.getAngles();

    object.rotateTo(targetAngles);
    Vector.release(targetAngles);

    if (Math.abs(objectAngles.getZ() - movingAngle) >= 15)
      return true;

    // Try move
    object.move();
    return true;
  }

  @SuppressWarnings("SimplifiableIfStatement")
  private boolean isPivotPointDeprecated()
  {
    if (pivotPoint == null)
      return false;

    float vectorLength = getLength(object, pivotPoint);
    if (vectorLength > influenceOfPivotPoint)
      return false;

    return pivotPointTimeLeft <= 0;
  }

  private void updatePivotPoint()
  {
    if (pivotPoint == null)
    {
      pivotPoint = Vector.getInstance(2, object.getPosition());
      pivotPointTimeLeft = pivotPointLifeTime;
      return;
    }

    float vectorLength = getLength(object, pivotPoint);
    if (vectorLength > influenceOfPivotPoint)
    {
      pivotPoint = Vector.getInstance(2, object.getPosition());
      pivotPointTimeLeft = pivotPointLifeTime;
      return;
    }

    pivotPointTimeLeft -= DeltaTime.get();
  }

  private static float getLength(WorldObject<?, ?> object, Vector2 point)
  {
    Vector2 position = Vector.getInstance(2, object.getPosition());
    Vector2 vector = position.getSubtract(point);
    float vectorLength = vector.getLength();

    Vector.release(vector);
    Vector.release(position);

    return vectorLength;
  }

  private Vector2 getMovingVector()
  {
    Vector2 objectPosition = Vector.getInstance(2, object.getPosition());
    Vector2 nextStepVector = Vector.getInstance(2);

    while (true)
    {
      // We arrived
      if (currentPathStep >= path.size())
      {
        Vector.release(objectPosition);
        return null;
      }

      Vector2 nextStep = path.get(currentPathStep);
      nextStepVector.setFrom(nextStep);
      nextStepVector.subtract(objectPosition);

      if (nextStepVector.getLength() > stepCompletion)
      {
        Vector.release(objectPosition);
        return nextStepVector;
      }

      currentPathStep++;
    }
  }

  @Override
  public void release()
  {
    Vector.release(path);
  }
}