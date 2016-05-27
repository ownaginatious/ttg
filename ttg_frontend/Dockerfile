FROM nginx:stable

ENV LAST_UPDATED = 2016-05-20
RUN apt-get update && apt-get install -y curl git
RUN curl -sL https://deb.nodesource.com/setup_4.x | bash - \
    && apt-get update && apt-get install -y nodejs
WORKDIR /code
COPY package.json ./
RUN npm install -no-color
COPY Gruntfile.js bower.json favicon.ico ./
COPY src/ ./src
RUN ./node_modules/grunt-cli/bin/grunt prod-build -no-color
COPY ./nginx.conf /etc/nginx/nginx.conf
WORKDIR /dist
RUN mv /code/dist /dist && rm -rf /code
