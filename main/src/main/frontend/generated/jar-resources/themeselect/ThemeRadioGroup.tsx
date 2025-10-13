import "./theme-radio-group";

type Props = {
  className?: string;
  label?: string;
};

export function ThemeRadioGroup({ className, label }: Props) {
  // @ts-ignore
  return <theme-radio-group class={className} label={label} />;
}
