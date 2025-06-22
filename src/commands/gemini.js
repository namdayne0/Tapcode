const { SlashCommandBuilder, ChannelType, PermissionsBitField, EmbedBuilder } = require('discord.js');
const { GoogleGenerativeAI, HarmBlockThreshold, HarmCategory } = require('@google/generative-ai');
const fs = require('node:fs');
const path = require('node:path');

// Đường dẫn đến file lưu trạng thái kênh Gemini
const CONFIG_FILE = path.join(__dirname, '..', 'gemini_config.json');

// Khởi tạo Gemini API
const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);
const model = genAI.getGenerativeModel({
    model: "gemini-2.0-flash", // Đảm bảo sử dụng model gemini-1.5-flash
    // Thêm System Instruction để định hướng cách Gemini trả lời
    system_instruction: "Bạn là một bot Discord thân thiện và hữu ích, được tạo ra để trả lời các câu hỏi và tương tác với người dùng. Hãy trả lời ngắn gọn, rõ ràng và vui vẻ. Đừng trả lời các câu hỏi về chính trị hoặc tôn giáo."
});

// Lưu trữ ID kênh Gemini được thiết lập cho mỗi guild (máy chủ)
let geminiChannels = {};

// Tải cấu hình khi bot khởi động
function loadConfig() {
    if (fs.existsSync(CONFIG_FILE)) {
        try {
            const rawData = fs.readFileSync(CONFIG_FILE, 'utf8');
            geminiChannels = JSON.parse(rawData);
        } catch (error) {
            console.error('Lỗi khi đọc hoặc phân tích gemini_config.json:', error);
            geminiChannels = {}; // Đặt lại nếu có lỗi để tránh crash
        }
    }
}

// Lưu cấu hình
function saveConfig() {
    try {
        fs.writeFileSync(CONFIG_FILE, JSON.stringify(geminiChannels, null, 2), 'utf8');
    } catch (error) {
        console.error('Lỗi khi lưu gemini_config.json:', error);
    }
}

// Tải cấu hình ngay khi file này được import
loadConfig();

// Lệnh /gemini-bot-setup
const setupCommand = {
    data: new SlashCommandBuilder()
        .setName('gemini-bot-setup')
        .setDescription('Thiết lập kênh hiện tại làm kênh trò chuyện với Gemini.')
        .setDMPermission(false), // Lệnh này không dùng trong DM

    async execute(interaction) {
        if (!interaction.guild) {
            return interaction.reply({ content: 'Lệnh này chỉ có thể sử dụng trong máy chủ (server).', ephemeral: true });
        }

        const guildId = interaction.guild.id;
        const channelId = interaction.channel.id;

        if (geminiChannels[guildId]) {
            return interaction.reply({ content: `Kênh Gemini đã được thiết lập tại <#${geminiChannels[guildId]}>. Hãy gỡ bỏ trước khi thiết lập lại.`, ephemeral: true });
        }

        // Kiểm tra quyền của bot trong kênh
        const botMember = interaction.guild.members.cache.get(interaction.client.user.id);
        if (!botMember.permissionsIn(interaction.channel).has(PermissionsBitField.Flags.SendMessages) ||
            !botMember.permissionsIn(interaction.channel).has(PermissionsBitField.Flags.ViewChannel)) {
            return interaction.reply({ content: 'Bot không có đủ quyền (Xem kênh, Gửi tin nhắn) trong kênh này để thiết lập Gemini.', ephemeral: true });
        }

        geminiChannels[guildId] = channelId;
        saveConfig();

        const embed = new EmbedBuilder()
            .setColor(0x00FF7F) // Màu xanh lá cây
            .setTitle('✨ Thiết lập Gemini thành công!')
            .setDescription(`Kênh <#${channelId}> đã được thiết lập làm kênh trò chuyện với Gemini.`)
            .addFields(
                { name: 'Cách sử dụng', value: 'Bất kỳ tin nhắn nào trong kênh này (không phải lệnh) sẽ được gửi đến Gemini để phản hồi.' },
                { name: 'Gỡ bỏ thiết lập', value: 'Sử dụng lệnh `/gemini-bot-unsetup`.' }
            )
            .setTimestamp()
            .setFooter({ text: 'Powered by Google Gemini' });

        await interaction.reply({ embeds: [embed] });
    },
};

// Lệnh /gemini-bot-unsetup
const unsetupCommand = {
    data: new SlashCommandBuilder()
        .setName('gemini-bot-unsetup')
        .setDescription('Gỡ bỏ kênh trò chuyện với Gemini đã thiết lập.')
        .setDMPermission(false),

    async execute(interaction) {
        if (!interaction.guild) {
            return interaction.reply({ content: 'Lệnh này chỉ có thể sử dụng trong máy chủ (server).', ephemeral: true });
        }

        const guildId = interaction.guild.id;

        if (!geminiChannels[guildId]) {
            return interaction.reply({ content: 'Không có kênh Gemini nào được thiết lập cho máy chủ này.', ephemeral: true });
        }

        const oldChannelId = geminiChannels[guildId];
        delete geminiChannels[guildId];
        saveConfig();

        const embed = new EmbedBuilder()
            .setColor(0xFF4500) // Màu cam
            .setTitle('✖️ Đã gỡ bỏ thiết lập Gemini!')
            .setDescription(`Kênh <#${oldChannelId}> không còn là kênh trò chuyện với Gemini nữa.`)
            .setTimestamp()
            .setFooter({ text: 'Powered by Google Gemini' });

        await interaction.reply({ embeds: [embed] });
    },
};

// Export cả hai lệnh và biến geminiChannels, model để index.js có thể truy cập
module.exports.setupCommand = setupCommand;
module.exports.unsetupCommand = unsetupCommand;
module.exports.geminiChannels = geminiChannels;
module.exports.model = model;