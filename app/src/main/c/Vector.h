#ifndef VECTOR_HEADER
#define VECTOR_HEADER

struct Vector
{
	void* pData;
	long long DataSize;
	long long Size;
};


struct Vector* CreateVector(long long DataSize);

void Release(struct Vector* vector);

int Size(struct Vector* vector);

void Push(struct Vector* vector, void* pData);

void* Get(struct Vector* vector, long long index);

void Clear(struct Vector* vector);



#endif
