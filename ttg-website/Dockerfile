FROM nginx:stable-alpine

RUN apk update && apk --no-cache add dnsmasq && \
    apk add -u python=2.7.11-r3 py-pip=8.1.2.r0 && \
    pip install supervisor &&\
    echo "[supervisord]" > /etc/supervisord.conf && \
    echo "nodaemon=true" >> /etc/supervisord.conf && \
    echo "[program:nginx]" >> /etc/supervisord.conf && \
    echo "command=/usr/bin/nginx -g daemon off;" >> /etc/supervisord.conf && \
    echo "[program:dnsmasq]" >> /etc/supervisord.conf && \
    echo "command=/usr/bin/dnsmasq -k;" >> /etc/supervisord.conf

CMD ["/usr/bin/supervisord"]
