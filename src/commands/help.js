const { SlashCommandBuilder, EmbedBuilder } = require('discord.js');
const fs = require('node:fs');
const path = require('node:path');

module.exports = {
    // Định nghĩa Slash Command
    data: new SlashCommandBuilder()
        .setName('help')
        .setDescription('Hiển thị danh sách các lệnh có sẵn của bot này.'), // Thay đổi mô tả nếu bạn muốn

    // Hàm thực thi khi lệnh được gọi
    async execute(interaction) {
        const commandsPath = path.join(__dirname);
        const commandFiles = fs.readdirSync(commandsPath).filter(file => file.endsWith('.js'));

        const embed = new EmbedBuilder()
            .setColor(0xFFFF00) // Màu vàng
            .setTitle('Danh Sách Lệnh')
            .setDescription('Các lệnh có sẵn cho bot:')
            .setTimestamp()
            .setFooter({ text: `Yêu cầu bởi ${interaction.user.tag}` });

        for (const file of commandFiles) {
            const command = require(path.join(commandsPath, file));
            if (command.data && command.data.name && command.data.description) {
                embed.addFields({ name: `/${command.data.name}`, value: command.data.description });
            }
        }

        await interaction.reply({ embeds: [embed], ephemeral: true }); // Chỉ hiển thị cho người dùng gọi lệnh
    },
};