FROM nginx:1.9

ENV LAST_UPDATED = 2016-05-20
RUN curl -sL https://deb.nodesource.com/setup_4.x | sudo -E bash - \
    && apt-get install -y nodejs
ADD . /code
WORKDIR /code
RUN npm install && grunt prod-build
COPY ./dist /usr/share/nginx/html
