package shared.services

import shared.models.Snake
import shared.models.Moves._

object MoveService {
  def isValidMove(snake: Snake, move: Move): Boolean = {
    val noTail = snake.tail.isEmpty
    val cantGoBackIfHasTail = inverse(snake.move) != move
    noTail || cantGoBackIfHasTail
  }

  def horizontalCoef(move: Move) = move match {
    case Left => -1
    case Right => 1
    case _ => 0
  }

  def verticalCoef(move: Move) = move match {
    case Up => -1
    case Down => 1
    case _ => 0
  }

  def inverse(move: Move): Move = move match {
    case Up => Down
    case Down => Up
    case Left => Right
    case Right => Left
  }
}