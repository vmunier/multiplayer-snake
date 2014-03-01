package game

import scala.scalajs.js
import js.Dynamic.{ global => g }
import org.scalajs.dom
import org.scalajs.dom.extensions._
import shared.models.Block
import shared.models.Snake

object Canvas {
  lazy val canvas = dom.document.createElement("canvas").cast[dom.HTMLCanvasElement]
  private lazy val ctx = canvas.getContext("2d").cast[dom.CanvasRenderingContext2D]

  // use a smaller inner window size value to prevent possible scrolling
  lazy val windowHeight = g.window.innerHeight * 0.98
  lazy val windowWidth = g.window.innerWidth * 0.98

  def init() = {
    canvas.width = Game.NbBlocksInWidth * Game.BlockSize
    canvas.height = Game.NbBlocksInHeight * Game.BlockSize
    g.document.body.appendChild(canvas)
    canvas
  }

  def render(playerNbEatenBlocks: Int, nonEmptyBlocks: Seq[Block]) = {
    // clear window
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    renderBlocks(nonEmptyBlocks)
    displayScore(playerNbEatenBlocks)

    if (Game.gameOver) {
      displayGameOver()
    }
  }

  private def displayScore(nbEatenBlocks: Int) = {
    ctx.fillStyle = "black"
    ctx.font = "20px Arial"
    ctx.textAlign = "left"
    ctx.textBaseline = "top"
    ctx.fillText("Blocks eaten: " + nbEatenBlocks, 32, 32)
  }

  private def displayGameOver() = {
    val (color, text) = if (Game.win) {
      ("green", "YOU WIN")
    } else {
      ("red", "GAME OVER")
    }
    ctx.fillStyle = color
    ctx.font = "60px Arial"
    ctx.textAlign = "center"
    ctx.textBaseline = "bottom"
    ctx.fillText(text, canvas.width / 2, canvas.height / 2)
  }

  private def renderBlocks(blocks: Seq[Block]) = {
    for (block <- blocks) {
      ctx.fillStyle = block.style
      ctx.fillRect(block.pos.x * Game.BlockSize, block.pos.y * Game.BlockSize, Game.BlockSize, Game.BlockSize)
    }
  }

}
