build:
	javac --release 8 *.java

run: build
	java -cp . Main 1
