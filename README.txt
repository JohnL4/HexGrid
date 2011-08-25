This is some Java code to display a grid of hexagons.  I hope to use
it as the basis for a variety of hex-based things (mostly games,
starting with GURPS combat).  The earlier version (check the commit
comments) was junk, but this one is a better base on which to work.

Improvements are forthcoming; this is mainly a big checkpoint because
I finally got the first pass working and I wanted to commit before
continuing work (and breaking it all).

General theory is in
src/com/how_hard_can_it_be/hexgrid/doc-files/coordinate-transformation.pdf. Note
that this is not in the "doc" directory tree.  "doc" is updated
occasionally, but "src" will contain the latest version.

I have added a dependency on the JAMA matrix package.  Get it at
http://math.nist.gov/javanumerics/jama/

