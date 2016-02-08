# Vision-Tracking-Test

Dependencies: OpenCV 3.1 
Include the jar (opencv/build/java/opencv-310.jar) file and the native library (opencv/build/java/[x86 or x64]) .dll file in your project.

See the targ folder for image examples. Currently only tested with image #9. A through D show the stages of filtering.
How it was filtered:
1) Create an HSV map of the image. The bright green turns to blue.
2) Filter out everything except the blue color channel.
3) Get rid of low levels of blue.
4) Generate "blobs" by finding clusters of the remaining blue area and determining the points of the contours around them.
5) Turn these blobs into convex shapes. (Consider a shape such as a star. The convex shape would be a pentagon with it's points at the tips of the star.)
6) Simplify the shape by getting rid of contour points that are too close to eachother or where the angle is close to 180 degrees (almost a straight line)
7) Keep only the shapes with 4 edges.
8) Keep only the shapes whose sum of interior angles add to less than 360.
