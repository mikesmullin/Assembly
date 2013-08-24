all:
	gcc -c hello.s
	ld -o hello hello.o
