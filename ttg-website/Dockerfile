FROM nginx:stable-alpine

RUN apk update && apk --no-cache add dnsmasq && \
    pip install supervisor &&\
    echo "[supervisord]" > /etc/supervisord.conf && \
    echo "nodaemon=true" >> /etc/supervisord.conf && \
    echo "[program:nginx]" >> /etc/supervisord.conf && \
    echo "command=/usr/bin/nginx -g daemon off;" >> /etc/supervisord.conf && \
    echo "[program:dnsmasq]" >> /etc/supervisord.conf && \
    echo "command=/usr/bin/dnsmasq -k;" >> /etc/supervisord.conf

CMD ["/usr/bin/supervisord"]
