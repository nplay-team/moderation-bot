FROM node:lts

WORKDIR /usr/src/app

COPY package*.json ./

RUN npm ci

COPY . .

RUN npm run build

RUN npx prisma generate

CMD [ "node", "dist/bot.js" ]
