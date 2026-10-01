import pika

params = pika.ConnectionParameters(
    host='localhost',
    port=5672,
    credentials=pika.PlainCredentials('admin','admin')
)

conn = pika.BlockingConnection(params)
ch = conn.channel()
# Declare exchange and queue and bind
ch.exchange_declare(exchange='cmd.direct', exchange_type='direct', durable=True)
ch.queue_declare(queue='test.email.queue', durable=True)
ch.queue_bind(queue='test.email.queue', exchange='cmd.direct', routing_key='email.send')
print('Queue and binding declared')
conn.close()
