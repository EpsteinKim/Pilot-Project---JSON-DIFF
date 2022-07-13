<template>
  <div>
    <v-container class="col-12 px-8">
      <v-row class="d-flex flex-column flex-sm-row justify-space-between align-center">
        <v-col class="text-h5 text-center text-sm-left">JSON DIFF</v-col>

        <v-col class="d-flex justify-space-around align-center">
          <v-row class="d-sm-flex text-body-1 justify-center justify-sm-end flex-nowrap">

            <v-chip class="red lighten-4 mx-1 dense">diff_type</v-chip>
            <v-chip class="yellow lighten-4 mx-1">diff_value</v-chip>
            <v-chip class="grey lighten-2 mx-1">not_key</v-chip>
          </v-row>
        </v-col>

      </v-row>
    </v-container>
  </div>
</template>

<script>
import colorObj from "@/assets/color";

export default {
  name: "headerDiv"
}
</script>

<style scoped>

</style>