package game

import shared.models.IdTypes._
import shared.models.Snake
import shared.models.Block

// Contains all the game vars values at a certain point in time
case class GameSnapshot(snakes: Map[SnakeId, Snake], losingSnakes: Map[SnakeId, Snake], foods: Set[Block], foodsInDigestion: Set[Block])
