FROM nginx:1.9

ENV LAST_UPDATED = 2016-05-20
RUN apt-get update && apt-get install -y curl
RUN curl -sL https://deb.nodesource.com/setup_4.x | bash - \
    && apt-get update && apt-get install -y nodejs
ADD . /code
WORKDIR /code
RUN npm install && ./node_modules/grunt-cli/bin/grunt prod-build
COPY ./dist /usr/share/nginx/html
