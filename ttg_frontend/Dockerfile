FROM nginx:stable

ENV LAST_UPDATED = 2016-05-20
RUN apt-get update && apt-get install -y curl git
RUN curl -sL https://deb.nodesource.com/setup_4.x | bash - \
    && apt-get update && apt-get install -y nodejs
WORKDIR /code
ADD bower.json package.json /code
RUN npm install -no-color && /node_modules/bower/bin/bower install
ADD Gruntfile.js favicon.ico src/ /code
RUN ./node_modules/grunt-cli/bin/grunt prod-build -no-color
RUN mv dist/* /usr/share/nginx/html
