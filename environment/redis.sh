docker run -d \
    --memory-swappiness=0 \
    --ulimit memlock=-1:-1 \
    -it -p 6379:6379 \
    -v ~/redis/data:/data \
    --name redis \
    redis:6.2.2
