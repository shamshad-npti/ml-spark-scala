// A simple comment
// val for constant value
val x = 100

println(s"Value of x=${x}")

// var for variable

var y = 100

println(s"Value of y=${y}")

// function

def mul(x: Int, y: Int): Int = x * y

println (mul(20, 20))

// assign function to a new value
val func = mul _

println(func(20, 30))

object Circle {
  def area(r: Double): Double = Math.PI * r * r
  def perimeter(r: Double): Double = 2.0 * Math.PI * r
}

println(Circle.area(10.0))
println(Circle.perimeter(10.0))
