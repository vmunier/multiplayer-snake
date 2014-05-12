package game

import scala.scalajs.js
import js.Dynamic.{ global => g }
import org.scalajs.dom
import org.scalajs.dom.extensions._
import shared.models.Block
import shared.models.Snake
import shared.models.GameConstants._

object Canvas {
  lazy val canvas = dom.document.createElement("canvas").cast[dom.HTMLCanvasElement]
  private lazy val ctx = canvas.getContext("2d").cast[dom.CanvasRenderingContext2D]

  // use a smaller inner window size value to prevent possible scrolling
  lazy val windowHeight = g.window.innerHeight * 0.98
  lazy val windowWidth = g.window.innerWidth * 0.98

  def init() = {
    canvas.width = NbBlocksInWidth * Game.BlockSize
    canvas.height = NbBlocksInHeight * Game.BlockSize

    g.jQuery(".gameArea").empty().append(canvas)
    canvas
  }

  def render(playerNbEatenBlocks: Int, maybeSnakeHead: Option[Block], nonEmptyBlocks: Seq[Block], gameOver: Boolean, gameLost: Boolean) = {
    // clear window
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    renderBlocks(nonEmptyBlocks)
    displayScore(playerNbEatenBlocks)

    for (block <- maybeSnakeHead) {
      renderSnakeHead(block)
    }

    if (gameOver || gameLost) {
      displayGameOver(gameLost)
    }
  }

  private def displayScore(nbEatenBlocks: Int) = {
    ctx.fillStyle = "black"
    ctx.font = "20px Arial"
    ctx.textAlign = "left"
    ctx.textBaseline = "top"
    ctx.fillText("Blocks eaten: " + nbEatenBlocks, 32, 32)
  }

  private def displayGameOver(gameLost: Boolean) = {
    val (color, text) = if (gameLost) {
      ("red", "DEFEAT")
    } else {
      ("green", "VICTORY")
    }
    ctx.fillStyle = color
    ctx.font = "60px Arial"
    ctx.textAlign = "center"
    ctx.textBaseline = "bottom"
    ctx.fillText(text, canvas.width / 2, canvas.height / 2)
  }

  private def renderSnakeHead(block: Block) = {
    ctx.strokeStyle = "black"
    ctx.lineWidth = 2
    ctx.strokeRect(block.pos.x * Game.BlockSize, block.pos.y * Game.BlockSize, Game.BlockSize, Game.BlockSize)
  }

  private def renderBlocks(blocks: Seq[Block]) = {
    for (block <- blocks) {
      renderBlock(block.pos.x * Game.BlockSize, block.pos.y * Game.BlockSize, Game.BlockSize, Game.BlockSize, block.style)
    }
  }

  private def renderBlock(x: Int, y: Int, width: Int, height: Int, style: String) = {
    ctx.fillStyle = style
    ctx.fillRect(x, y, width, height)
  }
}
