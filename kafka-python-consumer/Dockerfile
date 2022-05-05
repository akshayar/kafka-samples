#Deriving the latest base image
FROM python:latest


#Labels as key value pair
LABEL Maintainer="akshayar:sample-python-consumer"



COPY requirements.txt requirements.txt
RUN pip install --no-cache-dir -r requirements.txt



# Any working directory can be chosen as per choice like '/' or '/home' etc
# i have chosen /usr/app/src
WORKDIR /usr/app/src

#to COPY the remote file at working directory in container
COPY kafka-consumer.py ./
# Now the structure looks like this '/usr/app/src/test.py'


#CMD instruction should be used to run the software
#contained by your image, along with any arguments.

ENTRYPOINT [ "python","kafka-consumer.py"]