import pika

params = pika.ConnectionParameters(
    host='localhost',
    port=5672,
    credentials=pika.PlainCredentials('admin','admin')
)

conn = pika.BlockingConnection(params)
ch = conn.channel()

method, header, body = ch.basic_get(queue='test.email.queue', auto_ack=True)
if method:
    print('--- MESSAGE RECEIVED ---')
    print(body.decode())
else:
    print('No hay mensajes en test.email.queue')

conn.close()
