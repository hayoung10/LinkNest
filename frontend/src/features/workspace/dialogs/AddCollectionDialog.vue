<template>
  <Dialog :open="open" @update:open="onOpenChange">
    <DialogContent class="sm:max-w-[400px]">
      <form @submit.prevent="handleSubmit">
        <DialogHeader>
          <DialogTitle>새 컬렉션 추가</DialogTitle>
          <DialogDescription>
            새 컬렉션의 이름을 입력해주세요.
          </DialogDescription>
        </DialogHeader>

        <div class="space-y-4 py-4">
          <div class="space-y-2">
            <Label for="name">컬렉션 이름 *</Label>
            <Input
              id="name"
              v-model="name"
              placeholder="컬렉션 이름"
              required
            />
          </div>
        </div>

        <DialogFooter>
          <Button type="button" variant="outline" @click="onOpenChange(false)">
            취소
          </Button>
          <Button type="submit">추가</Button>
        </DialogFooter>
      </form>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onAdd: (name: string) => void;
}

const props = defineProps<Props>();

const name = ref("");

watch(
  () => props.open,
  (open) => {
    if (!open) name.value = "";
  }
);

function handleSubmit() {
  if (!name.value.trim()) return;
  props.onAdd(name.value.trim());
  props.onOpenChange(false);
}
</script>
