import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [],
  templateUrl: './page-header.component.html'
})
export default class PageHeaderComponent {
  title = input<string>('');
  subtitle = input<string>('');
  color = input<'blue' | 'pink' | 'green' | 'indigo'>('blue');
  
  colorClasses = computed(() => {
    switch (this.color()) {
      case 'pink':
        return 'from-pink-600 to-pink-800';
      case 'green':
        return 'from-green-600 to-green-800';
      case 'indigo':
        return 'from-indigo-600 to-indigo-800';
      default: // blue
        return 'from-blue-700 to-blue-900';
    }
  });
}
