# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the local code to the container
COPY . /app

# Install sbt (Scala Build Tool)
RUN apt-get update && apt-get install -y wget
RUN wget https://github.com/sbt/sbt/releases/download/v1.8.0/sbt-1.8.0.deb
RUN dpkg -i sbt-1.8.0.deb
RUN apt-get install -f

# Build the Scala application using sbt
RUN sbt clean compile

# Run the application
CMD ["sbt", "run"]

# Expose the port your application will listen on
EXPOSE 8080
