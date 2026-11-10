import { Component, computed, input } from '@angular/core';

type StatusType = 'success' | 'warning' | 'danger' | 'info' | 'primary' | 'secondary';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [],
  templateUrl: './status-badge.component.html'
})
export default class StatusBadgeComponent {
  status = input<StatusType>('primary');
  text = input<string>('');
  
  badgeClasses = computed(() => {
    switch (this.status()) {
      case 'success':
        return 'bg-green-100 text-green-800';
      case 'warning':
        return 'bg-yellow-100 text-yellow-800';
      case 'danger':
        return 'bg-red-100 text-red-800';
      case 'info':
        return 'bg-blue-100 text-blue-800';
      case 'secondary':
        return 'bg-gray-100 text-gray-800';
      default: // primary
        return 'bg-indigo-100 text-indigo-800';
    }
  });
}
