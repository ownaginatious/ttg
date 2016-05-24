FROM node:6

ADD . /code
WORKDIR /code
RUN npm install -no-color
CMD ["node", "./app.js", "--service", "ttg", "--server_port", "8080", "--max_size", "10000", "--db_host", "ttg-mongodb", "--db_port", "27017"]
