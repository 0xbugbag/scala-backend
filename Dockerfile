# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the local code to the container
COPY . /app

# Install necessary dependencies for sbt
RUN apt-get update && apt-get install -y curl gnupg

# Add the sbt repository and install sbt from the official repository
RUN echo "deb https://repo.scala-sbt.org/sbt/debian/ /" | tee -a /etc/apt/sources.list.d/sbt.list \
    && curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2E7489F70127B2F7" | tee /etc/apt/trusted.gpg.d/sbt.asc \
    && apt-get update \
    && apt-get install -y sbt

# Build the Scala application using sbt
RUN sbt clean compile

# Run the application
CMD ["sbt", "run"]

# Expose the port your application will listen on
EXPOSE 8080
