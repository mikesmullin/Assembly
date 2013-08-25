assemble:
	gcc -m64 -S -fno-ident test.c
	as --64 -g funct.s -o funct.o
#	as --64 funct.s -o funct.o
compile:
	gcc -o test test.s
	./test
	ld funct.o -o funct
	./funct
