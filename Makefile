assemble:
	gcc -m64 -S -fno-ident test.c
compile:
	gcc -o test test.s
	./test
