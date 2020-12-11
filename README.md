# kernelized webcam
by Evan Wang

## What is it
Kernelized webcam is an application which runs an (not necessarily a very fast) implementation of kernel image processing on the user's live webcam feed. Kernelized webcam then displays the resultant feed in a 640x320 `JFrame`. This project was made in 2 days after the author didn't get the score he wanted on his calculus 3 midterm.

## How to run
You can run kernelized webcam by typing `java WebcamProcessor` into your terminal, assuming you have the prequesite libraries installed (`sarxos' webcam-capture` and `slf4j-nop`). A `JFrame` displaying a live feed of your webcam should then be launched, as long as no other application is currently using your webcam and as long as `kernel.txt` is formatted correctly. `kernel.txt` should be formatted with the first line containing an integer `n`. The subsequent `n` lines should then contain each row of the intended kernel, with each `float` from the kernel being seperated by a space.
