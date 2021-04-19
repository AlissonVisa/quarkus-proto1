docker run -d --name mongodb \
  -v ~/mongo/data:/data/db \
  -p 27017:27017 \
  mongo:4.4.5