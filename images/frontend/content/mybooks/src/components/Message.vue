<template>
  <div class="box">
    Message:
    <slot></slot>
    <b>  {{ addendum }}</b>
  </div>
</template>

<script>
module.exports = {
  data: function () {
    return {
      addendum: ''
    }
  },
  created () {
    // called via event
    Event.$on('applied', (x) => console.log('got event in the message doohickey. Data>' + x))
  }
}
</script>

<style>
.box { background: yellow; border: solid red 1px; margin: 2em; padding: 1em; }
</style>
