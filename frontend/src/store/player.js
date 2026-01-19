// src/stores/player.js
import { defineStore } from "pinia"

export const usePlayerStore = defineStore("player", {
    state: () => ({
        queue: [],
        currentIndex: 0,
        playing: false,
        currentTime: 0,
        duration: 0,
        volume: 0.8,
        mode: "loop",       // loop | one | shuffle
    }),

    getters: {
        currentSong(state) {
            return state.queue[state.currentIndex] || null
        },
    },

    actions: {
        setQueue(list, index = 0) {
            this.queue = Array.isArray(list) ? list : []
            this.currentIndex = Math.max(0, Math.min(index, this.queue.length - 1))
            this.currentTime = 0
            this.duration = 0
        },

        setCurrentById(songId) {
            const idx = this.queue.findIndex(s => String(s.id) === String(songId))
            if (idx >= 0) this.currentIndex = idx
        },

        next() {
            if (!this.queue.length) return
            if (this.mode === "one") return // one 模式交给 ended 处理
            if (this.mode === "shuffle") {
                const r = Math.floor(Math.random() * this.queue.length)
                this.currentIndex = r
                return
            }
            this.currentIndex = (this.currentIndex + 1) % this.queue.length
        },

        prev() {
            if (!this.queue.length) return
            if (this.mode === "shuffle") {
                const r = Math.floor(Math.random() * this.queue.length)
                this.currentIndex = r
                return
            }
            this.currentIndex = (this.currentIndex - 1 + this.queue.length) % this.queue.length
        },

        toggleMode() {
            const order = ["loop", "one", "shuffle"]
            const i = order.indexOf(this.mode)
            this.mode = order[(i + 1) % order.length]
        },
    },
})
