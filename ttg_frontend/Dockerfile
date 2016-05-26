FROM nginx:stable

ENV LAST_UPDATED = 2016-05-20
RUN apt-get update && apt-get install -y curl git
RUN curl -sL https://deb.nodesource.com/setup_4.x | bash - \
    && apt-get update && apt-get install -y nodejs
WORKDIR /code
ADD package.json ./
RUN npm install -no-color
ADD Gruntfile.js bower.json favicon.ico src/ ./
RUN ./node_modules/grunt-cli/bin/grunt prod-build -no-color
RUN mv dist/* /usr/share/nginx/html
