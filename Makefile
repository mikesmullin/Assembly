all: write.o mmap.o socket.o daemon.o args.o

write.o:
	gcc -v -c write.S; \
	ld -s -o write write.o

mmap.o:
	gcc -c mmap.s; \
	ld -s -o mmap mmap.o

socket.o:
	gcc -c socket.s; \
	ld -s -o socket socket.o

daemon.o:
	gcc -c daemon.s; \
	ld -s -o daemon daemon.o

args.o:
	gcc -c args.s; \
	ld -s -o args args.o

hello: hello.asm
	make clean
	nasm -f elf64 hello.asm
	ld -s -o hello hello.o

clean:
	rm -f write mmap socket daemon args; \
	rm -f write.o mmap.o socket.o daemon.o args.o; \
	rm -f core
	rm -f hello

