FROM oven/bun:1

WORKDIR /app

COPY package.json bun.lock ./

RUN bun install

COPY . .

EXPOSE 4200

CMD ["bun", "run", "start"]