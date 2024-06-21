FROM python:3.11.7-slim
WORKDIR /app
COPY . /app
RUN pip install --no-cache-dir -r requirements.txt
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh
ENTRYPOINT ["entrypoint.sh"]
EXPOSE 5000