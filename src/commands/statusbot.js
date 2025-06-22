const { SlashCommandBuilder, EmbedBuilder } = require('discord.js');
const os = require('os'); // Để lấy thông tin hệ thống

module.exports = {
    data: new SlashCommandBuilder()
        .setName('statusbot')
        .setDescription('Hiển thị thông tin trạng thái của bot (CPU, RAM, thời gian hoạt động).'),
    async execute(interaction) {
        // Lấy thời gian hoạt động của bot
        const uptimeSeconds = process.uptime();
        const days = Math.floor(uptimeSeconds / (3600 * 24));
        const hours = Math.floor((uptimeSeconds % (3600 * 24)) / 3600);
        const minutes = Math.floor((uptimeSeconds % 3600) / 60);
        const seconds = Math.floor(uptimeSeconds % 60);
        const botUptime = `${days}d ${hours}h ${minutes}m ${seconds}s`;

        // Lấy thông tin RAM
        const totalMemory = os.totalmem(); // Tổng RAM hệ thống (bytes)
        const freeMemory = os.freemem();   // RAM trống hệ thống (bytes)
        const usedMemory = totalMemory - freeMemory;
        const nodeUsedMemory = process.memoryUsage().heapUsed; // RAM bot Node.js đang sử dụng

        // Chuyển đổi sang GB và MB
        const totalMemoryGB = (totalMemory / (1024 ** 3)).toFixed(2);
        const usedMemoryGB = (usedMemory / (1024 ** 3)).toFixed(2);
        const nodeUsedMemoryMB = (nodeUsedMemory / (1024 ** 2)).toFixed(2);

        // Lấy thông tin CPU
        const cpus = os.cpus();
        const cpuModel = cpus[0].model;
        const cpuCores = cpus.length;

        // Tính toán mức sử dụng CPU (load average)
        const loadAvg = os.loadavg(); // [1 phút, 5 phút, 15 phút]

        // Lấy ping của bot (độ trễ API)
        const ping = Math.round(interaction.client.ws.ping);

        // Tạo Embed
        const statusEmbed = new EmbedBuilder()
            .setColor(0x0099FF) // Màu xanh lam
            .setTitle('📊 Trạng Thái Bot')
            .setThumbnail(interaction.client.user.displayAvatarURL()) // Avatar của bot
            .addFields(
                { name: '🌐 Ping API', value: `${ping}ms`, inline: true },
                { name: '⏰ Thời gian hoạt động', value: `${botUptime}`, inline: true },
                { name: '\u200B', value: '\u200B', inline: true }, // Khoảng trống
                { name: '💻 CPU', value: `\`${cpuModel} (${cpuCores} cores)\``, inline: true },
                { name: '📈 Tải CPU (1/5/15 phút)', value: `\`${loadAvg[0].toFixed(2)} / ${loadAvg[1].toFixed(2)} / ${loadAvg[2].toFixed(2)}\``, inline: true },
                { name: '\u200B', value: '\u200B', inline: true }, // Khoảng trống
                { name: '💾 RAM Hệ Thống', value: `\`${usedMemoryGB}GB / ${totalMemoryGB}GB\``, inline: true },
                { name: '🧠 RAM Bot Đang Dùng', value: `\`${nodeUsedMemoryMB}MB\``, inline: true },
                { name: '\u200B', value: '\u200B', inline: true }, // Khoảng trống
            )
            .setTimestamp() // Thêm thời gian hiện tại
            .setFooter({ text: `Yêu cầu bởi ${interaction.user.tag}` });

        await interaction.reply({ embeds: [statusEmbed] });
    },
};