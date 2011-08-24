<TeXmacs|1.0.7.6>

<style|article>

<\body>
  <\doc-data|<doc-title|Coordinate Transformations Between Square and Hex
  Grids>>
    \;
  </doc-data>

  In a square grid, cell coordinates are simple. The origin square is
  <math|(0,0)>, the square immediately to the right of the origin cell is
  <math|(1,0)>, the square immediately above the origin cell is <math|(0,1)>,
  and so on.

  In a hex grid, we can do the same, but the axes must be tilted. The origin
  hex is <math|(0,0)>, the hex immediately to the right of the origin hex is
  <math|(1,0)>, the hex immediately to the "northeast" of the origin hex is
  <math|(0,1)>, and so on.

  We want a mathematical transformation between the two coordinate systems,
  to be used for the following purposes:

  <\itemize>
    <item>Hit-testing. We want to transform a mouse click (in magenta), in
    square-space coordinates (shown in maroon in Figure <reference|Axes and
    Coordinates>), to hex-space coordinates (shown in blue).

    <item>Rendering. We want to know where to draw a particular hex, given in
    hex-space coordinates, in square-space coordinates.
  </itemize>

  See this paper for basic change-of-basis matrices:
  <hlink|http://www.math.ucsd.edu/~nslingle/bases.pdf|>

  <big-figure|<postscript|file:///C:/Users/John/workspace/HexGrid2/src/com/how_hard_can_it_be/hexgrid/doc-files/hexgrid-coordinate-transform.png|567px|617px||||>|<label|Axes
  and Coordinates>>

  <section|Change-of-basis matrix>

  If the distance between hex centers is 1 unit (in GURPS, say, 1 yard), then
  r, the distance from a hex center to the center of any of its edges is

  <\equation*>
    r = 0.5
  </equation*>

  And <math|s>, which is both the distance from the hex's center to any of
  its vertices <em|and> the length of a side (since a hex is composed of
  equilateral triangles) is

  <\eqnarray*>
    <tformat|<table|<row|<cell|s>|<cell|=>|<cell|<frac|0.5|cos(<frac|\<pi\>|6>)>>>|<row|<cell|>|<cell|=>|<cell|<frac|1|2*cos(<frac|\<pi\>|6>)>>>|<row|<cell|>|<cell|\<cong\>>|<cell|0.577>>>>
  </eqnarray*>

  The basis vectors in what I will call ``hex space''
  (<math|\<bbb-H\><rsup|2>>) are given (in terms of the traditional Cartesian
  space <math|\<bbb-R\><rsup|2>>) as

  <\eqnarray*>
    <tformat|<table|<row|<cell|>|<cell|(1, 0)>|<cell|>>>>
  </eqnarray*>

  and

  <\eqnarray*>
    <tformat|<table|<row|<cell|(cos(<frac|\<pi\>|3>),
    sin(<frac|\<pi\>|3>))>|<cell|=>|<cell|(<frac|1|2>,
    <frac|<sqrt|3|>|2>)>>|<row|<cell|>|<cell|\<cong\>>|<cell|(0.5, 0.866)>>>>
  </eqnarray*>

  We write this as a matrix and multiply it by a vector in
  <math|\<bbb-H\><rsup|2>> to get a vector in <math|\<bbb-R\><rsup|2>>:

  <\equation*>
    M =<matrix|<tformat|<table|<row|<cell|1>|<cell|<frac|1|2>>>|<row|<cell|0>|<cell|<frac|<sqrt|3|>|2>>>>>>
  </equation*>

  <\eqnarray*>
    <tformat|<table|<row|<cell|<wide|x|\<vect\>><rsub|\<bbb-R\><rsup|2>>>|<cell|=>|<cell|M
    \<cdot\> <wide|x|\<vect\>><rsub|\<bbb-H\><rsup|2>>>>|<row|<cell|<matrix|<tformat|<table|<row|<cell|1>>|<row|<cell|0>>>>>>|<cell|=>|<cell|M
    \<cdot\> <matrix|<tformat|<table|<row|<cell|1>>|<row|<cell|0>>>>>>>|<row|<cell|<matrix|<tformat|<table|<row|<cell|<frac|1|2>>>|<row|<cell|<frac|<sqrt|3|>|2>>>>>>>|<cell|=>|<cell|M
    \<cdot\> <matrix|<tformat|<table|<row|<cell|0>>|<row|<cell|1>>>>>>>>>
  </eqnarray*>

  Inverting the matrix should get us a matrix capable of performing the
  opposite transformation (from <math|\<bbb-R\><rsup|2>> to
  <math|\<bbb-H\><rsup|2>>):

  <\eqnarray*>
    <tformat|<table|<row|<cell|M<rsup|-1>>|<cell|=>|<cell|<matrix|<tformat|<table|<row|<cell|1>|<cell|<frac|-1|<sqrt|3|>>>>|<row|<cell|0>|<cell|<frac|2|<sqrt|3|>>>>>>>>>>>
  </eqnarray*>

  <section|Hit-testing>

  The magenta point in Figure <reference|Axes and Coordinates> represents a
  mouse-click, <math|<wide|m|\<vect\>><rsub|\<bbb-R\><rsup|2>>>, whose
  coordinates arrive to us in <math|\<bbb-R\><rsup|2>>. \ We transform
  <math|<wide|m|\<vect\>>> to <math|\<bbb-H\><rsup|2>> by multiplying by
  <math|M<rsup|-1>>:

  <\equation*>
    <wide|m|\<vect\>><rsub|\<bbb-H\><rsup|2>> = M<rsup|-1> \<cdot\>
    <wide|m|\<vect\>><rsub|\<bbb-R\><rsup|2>>
  </equation*>

  We then round each coordinate of <math|<wide|m|\<vect\>><rsub|\<bbb-H\><rsup|2>>>
  both up and down to give four candidate ``closest hex centers'' (using
  <math|<wide|h|\<vect\>>> to represent <math|<wide|m|\<vect\>><rsub|\<bbb-H\><rsup|2>>>,
  <math|u> to represent the first basis vector of <math|\<bbb-H\><rsup|2>>,
  <math|v> to represent the second basis vector of <math|\<bbb-H\><rsup|2>>,
  to simplify the notation):

  <\eqnarray*>
    <tformat|<table|<row|<cell|>|<cell|(<left|lfloor>h<rsub|u><right|rfloor>,
    <left|lfloor>h<rsub|v><right|rfloor>)>|<cell|>>|<row|<cell|>|<cell|(<left|lceil>h<rsub|u><right|rceil>,
    <left|lceil>h<rsub|v><right|rceil>)>|<cell|>>|<row|<cell|>|<cell|(<left|lfloor>h<rsub|u><right|rfloor>,
    <left|lceil>h<rsub|v><right|rceil>)>|<cell|>>|<row|<cell|>|<cell|(<left|lceil>h<rsub|u><right|rceil>,
    <left|lfloor>h<rsub|v><right|rfloor>)>|<cell|>>>>
  </eqnarray*>

  We then multiply each of these four hex-center points by <math|M>, to
  transform them back to <math|\<bbb-R\><rsup|2>>, and use Pythagoras to
  measure the square of the distance of the mouse-click point to each of the
  four hex centers. \ For the first point <math|p<rsub|1>> in
  <math|\<bbb-R\><rsup|2>>:

  <\eqnarray*>
    <tformat|<table|<row|<cell|<wide|p|\<vect\>><rsub|1>>|<cell|=>|<cell|M
    \<cdot\> <wide|h|\<vect\>><rsub|1>>>|<row|<cell|r<rsub|1><rsup|>>|<cell|=
    >|<cell|<left|\|><wide|m|\<vect\>><rsub|\<bbb-R\><rsup|2>> -
    <wide|p|\<vect\>><rsub|1><right|\|><rsup|>>>>>
  </eqnarray*>

  And so on. \ Of the <math|r<rsub|1> \<ldots\> r<rsub|4>>, the smallest one
  corresponds to the closest hex center. (Note that in the software, we don't
  need to pay the cost of square root. \ Since we're just interested in the
  smallest value, we can use the square of the distance, since that's
  easier.)

  <section|Values>

  <\eqnarray*>
    <tformat|<table|<row|<cell|<frac|1|2*cos(<frac|\<pi\>|6>)>>|<cell|=>|<cell|<frac|1|<sqrt|3|>>>>|<row|<cell|>|<cell|\<cong\>>|<cell|0.577>>|<row|<cell|>|<cell|>|<cell|>>|<row|<cell|<frac|1|4*cos(<frac|\<pi\>|6>)>>|<cell|=>|<cell|<frac|1|2*<sqrt|3|>>>>|<row|<cell|>|<cell|\<cong\>>|<cell|0.289>>>>
  </eqnarray*>

  \;
</body>

<\initial>
  <\collection>
    <associate|par-mode|left>
  </collection>
</initial>

<\references>
  <\collection>
    <associate|Axes and Coordinates|<tuple|1|?>>
    <associate|auto-1|<tuple|1|?>>
    <associate|auto-2|<tuple|1|?>>
    <associate|auto-3|<tuple|2|?>>
    <associate|auto-4|<tuple|3|?>>
  </collection>
</references>

<\auxiliary>
  <\collection>
    <\associate|toc>
      <vspace*|1fn><with|font-series|<quote|bold>|math-font-series|<quote|bold>|1<space|2spc>Values>
      <datoms|<macro|x|<repeat|<arg|x>|<with|font-series|medium|<with|font-size|1|<space|0.2fn>.<space|0.2fn>>>>>|<htab|5mm>>
      <no-break><pageref|auto-1><vspace|0.5fn>
    </associate>
  </collection>
</auxiliary>